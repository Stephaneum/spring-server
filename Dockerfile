FROM node:10.24-alpine as stage-vue
WORKDIR /vue-build

# add vue dependencies
ADD src/main/vue/package.json .
ADD src/main/vue/package-lock.json .
RUN npm i

# build vue
ADD src/main/vue/ .
RUN npm run build

FROM gradle:7.4.2-jdk8 as stage-spring
WORKDIR /spring-build

# add vue build
COPY --from=stage-vue /vue-build/dist/ /spring-build/src/main/vue/dist/

# build spring
ADD . ./
RUN gradle bootJar
RUN mv build/libs/*.jar /server.jar
RUN rm -r /spring-build

# deploy
EXPOSE 9050
ENTRYPOINT ["java","-jar","/server.jar"]
