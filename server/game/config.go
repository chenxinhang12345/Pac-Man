package game

import (
	"encoding/json"
	"net"
	"sync"
)

// UsersLookUP stores all users infomaion
type UsersLookUP struct {
	Users map[int]User
	Mux   sync.RWMutex
}

// Users stores all users infomaion
var Users = UsersLookUP{
	Users: make(map[int]User),
}

// MoveInfo is the data scheme from the player
type MoveInfo struct {
	ID int
	X  int
	Y  int
}

// MovePacket stores move data and destination
type MovePacket struct {
	Addr net.Addr
	Move MoveInfo
}

// Food stores the food info
type Food struct {
	ID int
	X  int
	Y  int
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
	ID     int `json:"player_id"`
	FoodID int `json:"food_id"`
}

type Score struct {
	ID    int
	Score int
}

func (food Food) ToBytes() []byte {
	foodMarshal, err := json.Marshal(food)
	if err != nil {
		panic(err)
	}
	return foodMarshal
}

func (food Food) Tostring() string {
	return string(food.ToBytes())
}

func (foodsTable FoodsLookUP) ToStringList() []string {
	var foodList []string
	for _, v := range foodsTable.Foods {
		foodList = append(foodList, v.Tostring())
	}
	return foodList
}
