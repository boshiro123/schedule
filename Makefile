install:
	sudo apt install docker-compose \
	&& sudo usermod -aG docker $$USER \
	&& sudo service docker restart

mvn:
	mvn clean package



build:
	mvn clean package -DskipTests
	docker compose up --build

stop:
	docker compose stop

up:
	docker compose up -d

down:
	docker compose down

db-start:
	docker compose up -d db

db-stop:
	docker compose stop db









