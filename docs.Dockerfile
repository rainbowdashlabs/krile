FROM python:3.11-bullseye AS base

RUN pip install mkdocs-material && pip install mkdocs-git-revision-date-localized-plugin

COPY . .

RUN mkdocs build -f mkdocs.yml

FROM nginx:alpine

COPY --from=base /site /usr/share/nginx/html

EXPOSE 80
