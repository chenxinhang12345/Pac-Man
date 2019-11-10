package network

import (
	"Pac-Man/server/game"
	"fmt"
	"net"
)

var UDPServer net.PacketConn
var TCPServer net.Listener

func init() {
	_, err := net.ListenPacket("udp", ":1234")
	if err != nil {
		panic(err)
	}
	// logrus.Println(UDPServer.ReadFrom)
	TCPServer, err = net.Listen("tcp", ":4321")
	if err != nil {
		panic(err)
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

	user.MQ <- createMsgString("USERINFO", user.ToString())
	game.Users.Mux.RLock()
	for k, other := range game.Users.Users {
		if k == user.ID {
			continue
		}
		user.MQ <- createMsgString("NEWUSER", other.ToString())
		other.MQ <- createMsgString("NEWUSER", user.ToString())
	}
	game.Users.Mux.RUnlock()
	go user.HandleRead()
	go user.HandleWrite()
}

func createMsgString(header string, msg string) string {
	return fmt.Sprintf("%s;%s\n", header, msg)
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
