package game

import (
	"Pac-Man/server/maze"
	"encoding/json"
	"fmt"
	"math/rand"
	"strings"
	"time"

	"github.com/sirupsen/logrus"
)

func decodeTCPMsg(str string) {
	tokens := strings.Split(str, ";")
	if tokens[0] == "EAT" {
		var eatinfo EatInfo
		if err := json.Unmarshal([]byte(tokens[1]), &eatinfo); err != nil {
			logrus.Error(err)
		}
		handleEAT(eatinfo)
	} else if tokens[0] == "ATTACK" {
		var attackInfo AttackInfo
		if err := json.Unmarshal([]byte(tokens[1]), &attackInfo); err != nil {
			logrus.Error(err)
		}
		handleAttack(attackInfo)
	}
}

func handleAttack(attack AttackInfo) {
	logrus.Infof("Player %d was attacked", attack.PacmanID)
	Users.Mux.Lock()
	ghost := Users.Users[attack.GhostID]
	pacman := Users.Users[attack.PacmanID]
	ghost.Score += pacman.Score
	pacman.Score = 0
	Users.Users[attack.GhostID] = ghost
	Users.Users[attack.PacmanID] = pacman
	xCell := rand.Intn(maze.Width)
	yCell := rand.Intn(maze.Height)
	widthPart := MazeWidth / maze.Width
	heightPart := MazeHeight / maze.Height
	pacman.X = xCell*widthPart + widthPart/3
	pacman.Y = yCell*heightPart + heightPart/3
	pacman.TCPMQ <- createMsgString("POS", pacman.PosToString())
	var scoreList []string
	for _, v := range Users.Users {
		scoreList = append(scoreList, v.GetScoreString())
	}
	Users.Mux.Unlock()
	distributeScore(scoreList)
}

func handleEAT(eat EatInfo) {
	Foods.Mux.Lock()
	// When there is no such food, maybe it was eatten concurrently by another player
	if food, ok := Foods.Foods[eat.FoodID]; ok {
		// Delete original food
		delete(Foods.Foods, eat.FoodID)
		Foods.Mux.Unlock()
		if food.Type == "NORMAL" {
			logrus.Infof("Player %d eat normal food", eat.ID)
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
			food := generateFood("NORMAL")
			Foods.AddFood(food)
			foodList := Foods.ToStringList()
			DistributeFood(foodList)
			distributeAddFood(food)
		} else if food.Type == "INVISIBLE" {
			logrus.Infof("Player %d eat invisible food", eat.ID)
			Users.Mux.Lock()
			user := Users.Users[eat.ID]
			user.Visible = false
			user.InvisibleTimer.Stop()
			user.InvisibleTimer.Reset(3 * time.Second)
			Users.Mux.Unlock()
			food := generateFood("INVISIBLE")
			Foods.AddFood(food)
			foodList := Foods.ToStringList()
			DistributeFood(foodList)
			distributeAddFood(food)
		}
	} else {
		Foods.Mux.Unlock()
	}
}

func generateFood(Type string) Food {
	xCell := rand.Intn(maze.Width)
	yCell := rand.Intn(maze.Height)
	widthPart := MazeWidth / maze.Width
	heightPart := MazeHeight / maze.Height
	food := Food{
		ID:   rand.Intn(200),
		X:    xCell*widthPart + widthPart/2,
		Y:    yCell*heightPart + heightPart/2,
		Type: Type,
	}
	return food
}

func distributeAddFood(food Food) {
	Users.Mux.Lock()
	for _, user := range Users.Users {
		user.TCPMQ <- createMsgString("ADDFOOD", food.ToString())
	}
	Users.Mux.Unlock()
}

// DistributeFood is used at the beginning of the game (required by the Frontend)
// It will pass the food list to each player
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

// InitializeFood is to create 50 foods at the beginning of the game
func InitializeFood() {
	Foods.Mux.Lock()
	for i := 0; i < 50; i++ {
		food := generateFood("NORMAL")
		Foods.Foods[food.ID] = food
	}
	for i := 0; i < 5; i++ {
		food := generateFood("INVISIBLE")
		Foods.Foods[food.ID] = food
	}
	Foods.Mux.Unlock()
	logrus.Infoln("Successfully initialized food.")
}

// InitializeMaze is to create the new maze at the beginning of the game.
func InitializeMaze() {
	Maze = maze.NewMaze()
	Maze.SetUp()
}
