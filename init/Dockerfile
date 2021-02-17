# To build docker image:
# docker build -t websitecd/content-git-init .
#
FROM node:14 AS stage1

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
FROM node:14-slim

COPY --from=stage1 /usr/src/app /usr/src/app

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    git \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src/app

CMD [ "node", "init-content.js" ]