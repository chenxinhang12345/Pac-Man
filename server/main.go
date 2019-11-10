package main

import (
	"Pac-Man/server/network"
)

func main() {
	go network.TCPListen()
	select {}
}
