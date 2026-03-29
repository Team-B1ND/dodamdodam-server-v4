FROM node:20-alpine
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --omit=dev
COPY dist ./dist
COPY proto ./proto
EXPOSE 8080
CMD ["node", "dist/main.js"]
