package main

import (
	"fmt"
	"net"
)

func main() {
	server, err := net.ListenUDP("udp", &net.UDPAddr{Port: 1234})
	if err != nil {
		panic(err)
	}
	for {
		buf := make([]byte, 1024)
		n, addr, err := server.ReadFromUDP(buf)
		if err != nil {
			panic(err)
		}
		fmt.Println(string(buf[:n]), " from ", addr.String())
	}
}
