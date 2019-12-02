package game

import (
	"Pac-Man/server/maze"
	"encoding/json"
	"net"
	"sync"
	"time"
)

const (
	// MazeHeight is the pixel length of the col
	MazeHeight int = 1500
	// MazeWidth is the pixel length of the row
	MazeWidth int = 1300
	// InvisibleTime is the duration player invisible
	InvisibleTime time.Duration = 3
	// GameTime is the time duration for each game
	GameTime time.Duration = 1
)

// UsersLookUP stores all users infomaion
type UsersLookUP struct {
	Users map[int]*User
	Mux   sync.RWMutex
}

// Users stores all users infomaion
var Users = UsersLookUP{
	Users: make(map[int]*User),
}

// MoveInfo is the data scheme from the player
type MoveInfo struct {
	ID      int
	X       int
	Y       int
	Visible bool
}

// MovePacket stores move data and destination
type MovePacket struct {
	Addr net.Addr
	Move MoveInfo
}

// Food stores the food info
type Food struct {
	ID   int
	X    int
	Y    int
	Type string
}

// FoodsLookUP stores the food info
type FoodsLookUP struct {
	Foods map[int]Food
	Mux   sync.RWMutex
}

// Foods stores the food info
var Foods = FoodsLookUP{
	Foods: make(map[int]Food),
}

// EatInfo is the data scheme from the player
type EatInfo struct {
	ID     int
	FoodID int
}

// Score is to transmiss to the player data structre
type Score struct {
	ID    int
	Score int
}

// AttackInfo is the data scheme from the player
type AttackInfo struct {
	GhostID  int
	PacmanID int
}

// Result is to send the result of the game
type Result struct {
	Pacman int
	Ghost  int
}

// ToBytes is to create serialized food data
func (food Food) ToBytes() []byte {
	foodMarshal, err := json.Marshal(food)
	if err != nil {
		panic(err)
	}
	return foodMarshal
}

// ToString is to convert serialized food data to string
func (food Food) ToString() string {
	return string(food.ToBytes())
}

// ToStringList is to create a food list, which will be used at the start of the game
func (foodsTable *FoodsLookUP) ToStringList() []string {
	foodsTable.Mux.RLock()
	var foodList []string
	for _, v := range foodsTable.Foods {
		foodList = append(foodList, v.ToString())
	}
	foodsTable.Mux.RUnlock()
	return foodList
}

// AddFood is to add a new food to the exisiting list
func (foodsTable *FoodsLookUP) AddFood(food Food) {
	foodsTable.Mux.Lock()
	Foods.Foods[food.ID] = food
	foodsTable.Mux.Unlock()
}

// Maze is the main data structure to the walls
var Maze *maze.Maze

// GlobalTimer is to count the time of a game
var GlobalTimer *time.Timer
