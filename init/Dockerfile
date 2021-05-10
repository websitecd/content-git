# To build docker image:
# docker build -t spaship/content-git-init .
#
FROM node:16 AS stage1

# Create app directory
WORKDIR /usr/src/app

# Install app dependencies
# A wildcard is used to ensure both package.json AND package-lock.json are copied
# where available (npm@5+)
COPY package*.json ./

RUN npm install
# If you are building your code for production
# RUN npm ci --only=production

# Bundle app source
COPY *.js ./

CMD [ "node", "init-content.js" ]


# prod stage
FROM node:16-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    git \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

COPY --from=stage1 --chown=1001:root /usr/src/app /work

USER 1001

CMD [ "node", "init-content.js" ]