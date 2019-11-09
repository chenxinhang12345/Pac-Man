package network

import (
	"Pac-Man/server/game"
	"net"
)

var UDPServer net.PacketConn
var TCPServer net.Listener

func init() {
	_, err := net.ListenPacket("udp", ":1234")
	if err != nil {
		panic(err)
	}
	TCPServer, err = net.Listen("tcp", ":4321")
	if err != nil {
		panic(err)
	}

}

func TCPListen() {
	conn, err := TCPServer.Accept()
	if err != nil {
		panic(err)
	}
	go handleTCP(conn)
}

func handleTCP(conn net.Conn) {
	user := game.NewUser(conn)

	user.MQ <- string(user.ToString())
	go user.HandleRead()
	go user.HandleWrite()
}
