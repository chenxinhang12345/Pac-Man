package game

import (
	"Pac-Man/server/maze"
	"bufio"
	"encoding/json"
	"io"
	"math/rand"
	"net"
	"time"

	"github.com/sirupsen/logrus"
)

// User is to store all information about a player
type User struct {
	ID    int
	X     int
	Y     int
	Color int
	Score int
	Type  string
	Conn  net.Conn
	TCPMQ chan string
}

// NewUser will create a new user according to the connection
// User will keep alive the connection
func NewUser(conn net.Conn) User {
	rand.Seed(int64(time.Now().Nanosecond()))
	id := rand.Intn(1000)
	Users.Mux.Lock()
	for _, ok := Users.Users[id]; ok == true; id = rand.Intn(1000) {
		_, ok = Users.Users[id]
	}
	xCell := rand.Intn(maze.Width)
	yCell := rand.Intn(maze.Height)
	widthPart := MazeWidth / maze.Width
	heightPart := MazeHeight / maze.Height
	user := User{
		ID:    id,
		X:     xCell*widthPart + widthPart/3,
		Y:     yCell*heightPart + heightPart/3,
		Type:  "PACMAN",
		Conn:  conn,
		Color: -rand.Intn(16777215),
		TCPMQ: make(chan string, 1024),
	}
	// if len(Users.Users) != 0 && len(Users.Users)%2 == 0 {
	// 	user.Type = "GHOST"
	// }
	// TEST CODE
	if len(Users.Users) == 1 {
		user.Type = "GHOST"
	}
	Users.Users[id] = user
	Users.Mux.Unlock()
	return user
}

// HandleRead is to continuly read from the TCP connection.
func (user User) HandleRead() {
	reader := bufio.NewReader(user.Conn)
	for {
		user.Conn.SetReadDeadline(time.Now().Add(600 * time.Second))
		str, err := reader.ReadString('\n')
		if err != nil {
			if err == io.EOF {
				user.Conn.Close()
				return
			}
			logrus.Errorln("Error when read from TCP", err)
		}
		decodeTCPMsg(str)
	}
}

// HandleWrite is to write the msg from channel to TCP connection
// Each user will have a distinct TCP channel queue.
func (user User) HandleWrite() {
	writer := bufio.NewWriter(user.Conn)
	for {
		select {
		case msg := <-user.TCPMQ:
			writer.Write([]byte(msg))
			writer.Flush()
		}
	}
}

// ToString is to format user data to string
func (user User) ToString() string {
	return string(user.ToBytes())
}

// ToBytes is to format user data to bytes
func (user User) ToBytes() []byte {
	info := struct {
		ID    int
		Color int
		X     int
		Y     int
		Score int
		Type  string
	}{
		ID:    user.ID,
		Color: user.Color,
		X:     user.X,
		Y:     user.Y,
		Score: user.Score,
		Type:  user.Type,
	}
	userMarshal, err := json.Marshal(info)
	if err != nil {
		panic(err)
	}
	return userMarshal
}

// GetScoreBytes is to format user score data to bytes
func (user User) GetScoreBytes() []byte {
	score := Score{
		ID:    user.ID,
		Score: user.Score,
	}
	bytes, err := json.Marshal(score)
	if err != nil {
		panic(err)
	}
	return bytes
}

// GetScoreString is to format user score data to string
func (user User) GetScoreString() string {
	return string(user.GetScoreBytes())
}
