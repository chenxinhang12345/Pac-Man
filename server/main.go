package main

import (
	"Pac-Man/server/network"
)

func main() {
	network.TCPListen()
	select {}
}
