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

func init() {
	UDPServer, err := net.ListenPacket("udp", ":1234")
	if err != nil {
		panic(err)
	}
	for i := 0; i < 10; i++ {
		go UDPListen(UDPServer)
	}
	TCPServer, err = net.Listen("tcp", ":4321")
	if err != nil {
		panic(err)
	}
}

func UDPListen(UDPServer net.PacketConn) {
	buffer := make([]byte, 1024)
	for {
		n, addr, err := UDPServer.ReadFrom(buffer)
		if err != nil {
			logrus.Errorf("Error from %s, %s", addr.String(), err)
		}
		decodeUDP(buffer[:n], addr)
	}

}

func TCPListen() {
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
	fmt.Println(conn.RemoteAddr().String())
	user.TCPMQ <- createMsgString("USERINFO", user.ToString())
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
		err := json.Unmarshal([]byte(tokens[1]), &move)
		if err != nil {
			logrus.Error(err)
		}
		game.Users.Mux.Lock()
		user := game.Users.Users[move.ID]
		user.MoveMQ <- tokens[1]
		user.UDPaddr = addr
		game.Users.Mux.Unlock()
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
