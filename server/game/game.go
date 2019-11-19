package game

import (
	"Pac-Man/server/maze"
	"encoding/json"
	"fmt"
	"math/rand"
	"strings"

	"github.com/sirupsen/logrus"
)

func decodeTCPMsg(str string) {
	tokens := strings.Split(str, ";")
	if tokens[0] == "EAT" {
		var eatinfo EatInfo
		if err := json.Unmarshal([]byte(tokens[1]), &eatinfo); err != nil {
			logrus.Error(err)
		}
		fmt.Println(eatinfo)
		handleEAT(eatinfo)
	}
}

func handleEAT(eat EatInfo) {
	Foods.Mux.Lock()
	// When there is no such food, maybe it was eatten concurrently by another player
	if _, ok := Foods.Foods[eat.FoodID]; ok {
		delete(Foods.Foods, eat.FoodID)
		Users.Mux.Lock()
		user := Users.Users[eat.ID]
		user.Score++
		Users.Users[eat.ID] = user
		var scoreList []string
		for _, v := range Users.Users {
			scoreList = append(scoreList, v.GetScoreString())
		}
		Users.Mux.Unlock()
		distributeScore(scoreList)
		food := generateFood()
		Foods.Foods[food.ID] = food
		foodList := Foods.ToStringList()
		DistributeFood(foodList)
		distributeAddFood(food)
	}
	Foods.Mux.Unlock()
}

func generateFood() Food {
	food := Food{
		ID: rand.Intn(200),
		X:  rand.Intn(1000),
		Y:  rand.Intn(1000),
	}
	return food
}

func distributeAddFood(food Food) {
	bytes, err := json.Marshal(food)
	if err != nil {
		logrus.Error(err)
	}
	Users.Mux.Lock()
	for _, user := range Users.Users {
		user.TCPMQ <- createMsgString("ADDFOOD", string(bytes))
	}
	Users.Mux.Unlock()
}

func DistributeFood(foodList []string) {
	foodListBytes, err := json.Marshal(foodList)
	if err != nil {
		logrus.Error(err)
	}
	Users.Mux.Lock()
	for _, user := range Users.Users {
		user.TCPMQ <- createMsgString("FOOD", string(foodListBytes))
	}
	Users.Mux.Unlock()
}

func distributeScore(scoreList []string) {
	scoreListBytes, err := json.Marshal(scoreList)
	if err != nil {
		logrus.Error(err)
	}
	Users.Mux.Lock()
	for _, user := range Users.Users {
		user.TCPMQ <- createMsgString("SCORE", string(scoreListBytes))
	}
	Users.Mux.Unlock()
}

func createMsgString(header string, msg string) string {
	return fmt.Sprintf("%s;%s\n", header, msg)
}

func InitializeFood() {
	Foods.Mux.Lock()
	for i := 0; i < 50; i++ {
		food := generateFood()
		Foods.Foods[food.ID] = food
	}
	Foods.Mux.Unlock()
}

func InitializeMaze() {
	Maze = maze.NewMaze()
	Maze.SetUp()
}
