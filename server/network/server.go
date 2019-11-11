package network

import (
	"Pac-Man/server/game"
	"encoding/json"
	"fmt"
	"net"
	"strings"

	"github.com/sirupsen/logrus"
)

var UDPServer net.PacketConn
var TCPServer net.Listener
var udpTable = map[int]net.Addr{}
var udpMQ chan game.MovePacket

func init() {
	UDPServer, err := net.ListenPacket("udp", ":1234")
	if err != nil {
		panic(err)
	}
	for i := 0; i < 10; i++ {
		go UDPListen(UDPServer)
		go UDPWrite(UDPServer)
	}
	TCPServer, err = net.Listen("tcp", ":4321")
	if err != nil {
		panic(err)
	}
	udpMQ = make(chan game.MovePacket, 1024)

}

func UDPWrite(UDPServer net.PacketConn) {
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

func UDPListen(UDPServer net.PacketConn) {
	buffer := make([]byte, 1024)
	for {
		n, addr, err := UDPServer.ReadFrom(buffer)
		if err != nil {
			logrus.Errorf("Error from %s, %s", addr.String(), err)
		}
		fmt.Println(string(buffer[:n]))
		decodeUDP(buffer[:n], addr)
	}

}

func TCPListen() {
	game.InitializeFood()
	for {
		conn, err := TCPServer.Accept()
		if err != nil {
			panic(err)
		}
		go handleTCP(conn)
	}
}

func handleTCP(conn net.Conn) {
	user := game.NewUser(conn)
	logrus.Infof("A new player come in: %s\n", conn.RemoteAddr())
	user.TCPMQ <- createMsgString("USERINFO", user.ToString())
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
}

func createMsgString(header string, msg string) string {
	return fmt.Sprintf("%s;%s\n", header, msg)
}

func decodeUDP(bytes []byte, addr net.Addr) {
	str := string(bytes)
	tokens := strings.Split(str, ";")
	if tokens[0] == "POS" {
		var move game.MoveInfo
		if err := json.Unmarshal([]byte(tokens[1]), &move); err != nil {
			logrus.Error(err)
		}
		udpTable[move.ID] = addr
		distributeMove(tokens[1])
	}
}

func distributeMove(move string) {
	var moveInfo game.MoveInfo
	if err := json.Unmarshal([]byte(move), &moveInfo); err != nil {
		logrus.Error(err)
	}
	fmt.Println(udpTable)
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

// func createMsgString(header string, msg string) string {
// 	// Append header verb
// 	data := header + "\n"
// 	// Append msg length
// 	length := make([]byte, 8)
// 	binary.BigEndian.PutUint64(length, uint64(len(msg)))
// 	data = string(append([]byte(data), length...))
// 	// Append msg
// 	data += msg
// 	return data
// }
