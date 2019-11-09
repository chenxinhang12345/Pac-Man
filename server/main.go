package main

import (
	"net"
)

func main() {
	server, err := net.ListenPacket("udp", ":1234")
	if err != nil {
		panic(err)
	}
	for {
		buf := make([]byte, 1024)
		n, addr, err := server.ReadFrom(buf)
		if err != nil {
			panic(err)
		}
		server.WriteTo(buf[:n], addr)
	}
}
