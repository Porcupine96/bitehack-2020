up:
	docker-compose -f postgres.yaml up

down:
	docker-compose -f postgres.yaml down

restart:
	docker-compose -f postgres.yaml down && docker-compose -f postgres.yaml up

check:
	.git/hooks/pre-commit
