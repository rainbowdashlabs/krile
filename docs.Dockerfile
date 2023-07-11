FROM python:3.11-bullseye AS base

RUN pip install mkdocs-material && pip install mkdocs-git-revision-date-localized-plugin

COPY docs .

RUN find docs/ -type f -print0 | xargs -0 sed -i 's/★/:material-star:/g'
RUN find docs/ -type f -print0 | xargs -0 sed -i 's/⯪/:material-star-half-full:/g'
RUN find docs/ -type f -print0 | xargs -0 sed -i 's/☆/:material-star-outline:/g'

RUN mkdocs build -f mkdocs.yml

FROM nginx:alpine

COPY --from=base /site /usr/share/nginx/html

EXPOSE 80
