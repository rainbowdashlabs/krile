FROM python:3.11

RUN pip install mkdocs-material && pip install mkdocs-git-revision-date-localized-plugin

EXPOSE 80

WORKDIR /docs

COPY mkdocs.yml mkdocs.yml

COPY docs /docs/

COPY .git/ .git/

ENTRYPOINT ["mkdocs", "serve", "-a", "0.0.0.0:80"]
