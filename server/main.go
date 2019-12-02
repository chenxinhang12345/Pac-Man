package main

import (
	"Pac-Man/server/network"
)

func main() {
	go network.TCPListen()
	defer network.TCPServer.Close()
	defer network.UDPServer.Close()
	select {}
}
