package game

import "sync"

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
