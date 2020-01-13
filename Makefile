SHELL := /bin/bash
nginx_container := hulk-me

nginx:
	@docker run --name $(nginx_container) --rm -d -p 8080:80 nginx

nginx-stop:
	@docker stop $(nginx_container)

nginx-logs: nginx
	@docker logs -f $(nginx_container)