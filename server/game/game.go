package game

import (
	"net"
	"sync"
)

type UsersLookUP struct {
	Users map[int]User
	Mux   sync.RWMutex
}

var Users = UsersLookUP{
	Users: make(map[int]User),
}

type MoveInfo struct {
	ID int
	X  int
	Y  int
}

type MovePacket struct {
	Addr net.Addr
	Move MoveInfo
}
