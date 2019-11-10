package game

import (
	"bufio"
	"encoding/json"
	"math/rand"
	"net"

	"github.com/sirupsen/logrus"
)

type User struct {
	ID    int
	X     int
	Y     int
	Color int
	Score int
	Conn  net.Conn
	MQ    chan string
}

func NewUser(conn net.Conn) User {
	id := rand.Int()
	for _, ok := Users[id]; ok == true; id = rand.Int() {
		_, ok = Users[id]
	}
	user := User{
		ID:    id % 1000,
		X:     rand.Int() % 200,
		Y:     rand.Int() % 200,
		Conn:  conn,
		Color: 0,
		MQ:    make(chan string, 1024),
	}
	Users[id] = user
	return user
}

func (user User) HandleRead() {
	reader := bufio.NewReader(user.Conn)
	for {
		str, err := reader.ReadString('\n')
		if err != nil {
			logrus.Errorln("Error when read from TCP")
		}
		logrus.Println(str)
	}
}

func (user User) HandleWrite() {
	writer := bufio.NewWriter(user.Conn)
	select {
	case msg := <-user.MQ:
		writer.Write([]byte(msg))
		writer.Flush()
	}
}

func (user User) ToString() string {
	info := struct {
		ID    int
		Color int
		X     int
		Y     int
		Score int
	}{
		ID:    user.ID,
		Color: user.Color,
		X:     user.X,
		Y:     user.Y,
		Score: user.Score,
	}
	userMarshal, err := json.Marshal(info)
	if err != nil {
		panic(err)
	}
	return string(userMarshal)
}
