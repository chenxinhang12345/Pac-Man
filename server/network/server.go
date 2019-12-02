package network

import (
	"Pac-Man/server/game"
	"encoding/json"
	"fmt"
	"net"
	"strings"
	"time"

	"github.com/sirupsen/logrus"
)

// UDPServer is the listening entry point for UDP packets
var UDPServer net.PacketConn

// TCPServer is the listening entry point for TCP connection
var TCPServer net.Listener
var udpTable = map[int]net.Addr{}
var udpMQ = make(chan game.MovePacket, 1024)

func init() {
	UDPServer, err := net.ListenPacket("udp", ":1234")
	if err != nil {
		panic(err)
	}
	// Create total 20 goroutines for UDP read/write
	for i := 0; i < 10; i++ {
		go UDPListen(UDPServer)
		go UDPWrite(UDPServer)
	}
	TCPServer, err = net.Listen("tcp", ":4321")
	if err != nil {
		panic(err)
	}
}

// UDPWrite receive msgs from the channel
func UDPWrite(UDPServer net.PacketConn) {
	// This function is run by a goroutine
	// It will continuely read data from channel
	// All data gram via UDP should go through this channel
	for {
		select {
		case msg := <-udpMQ:
			move := msg.Move
			bytes, err := json.Marshal(move)
			if err != nil {
				logrus.Error(err)
			}
			UDPServer.WriteTo(bytes, msg.Addr)
		}
	}
}

// UDPListen will read from udp packets
func UDPListen(UDPServer net.PacketConn) {
	// This function is run by a goroutine
	// It will continuely read data from UDP
	buffer := make([]byte, 1024)
	for {
		n, addr, err := UDPServer.ReadFrom(buffer)
		if err != nil {
			logrus.Errorf("Error from %s, %s", addr.String(), err)
		}
		decodeUDP(buffer[:n], addr)
	}

}

// TCPListen will accept any connectin and start a new routine for IO
func TCPListen() {
	game.InitializeFood()
	game.InitializeMaze()
	for {
		conn, err := TCPServer.Accept()
		if err != nil {
			panic(err)
		}
		go handleTCP(conn)
	}
}

// handleTCP will handle each connection accepted by TCP via separate goroutine
func handleTCP(conn net.Conn) {
	user := game.NewUser(conn)
	logrus.Infof("A new player come in: %s\n", conn.RemoteAddr())
	user.TCPMQ <- createMsgString("USERINFO", user.ToString())
	user.TCPMQ <- createMsgString("MAZE", game.Maze.ToString())
	foodListString := game.Foods.ToStringList()
	game.DistributeFood(foodListString)
	game.Users.Mux.Lock()
	for k, other := range game.Users.Users {
		if k == user.ID {
			continue
		}
		user.TCPMQ <- createMsgString("NEWUSER", other.ToString())
		other.TCPMQ <- createMsgString("NEWUSER", user.ToString())
	}
	game.Users.Mux.Unlock()
	go user.HandleRead()
	go user.HandleWrite()
	go user.HandleInvisibleTimer()
	// The global counter will work when there are at least two players
	if len(game.Users.Users) == 1 {
		return
	}
	// When comming in a new user, reset the counter
	if game.GlobalTimer == nil {
		game.GlobalTimer = time.NewTimer(game.GameTime * time.Minute)
		go game.HandleGameTime()
		logrus.Infoln("Game start.")
	} else {
		game.GlobalTimer.Reset(game.GameTime * time.Minute)
		logrus.Infoln("Game counter reset.")
	}
}

// creteMsgString will construct a msg with the header and msg
func createMsgString(header string, msg string) string {
	return fmt.Sprintf("%s;%s\n", header, msg)
}

// decodeUDP will decode the UDP packet and find the data info
func decodeUDP(bytes []byte, addr net.Addr) {
	str := string(bytes)
	tokens := strings.Split(str, ";")
	if tokens[0] == "POS" {
		var move game.MoveInfo
		if err := json.Unmarshal([]byte(tokens[1]), &move); err != nil {
			logrus.Error(err)
		}
		if _, ok := udpTable[move.ID]; !ok {
			udpTable[move.ID] = addr
		}
		distributeMove(tokens[1])
	}
}

// distributeMove will pass-on player's move to other players
func distributeMove(move string) {
	var moveInfo game.MoveInfo
	if err := json.Unmarshal([]byte(move), &moveInfo); err != nil {
		logrus.Error(err)
	}
	for k, v := range udpTable {
		if k == moveInfo.ID {
			continue
		}
		msg := game.MovePacket{
			Addr: v,
			Move: moveInfo,
		}
		udpMQ <- msg
	}
}
