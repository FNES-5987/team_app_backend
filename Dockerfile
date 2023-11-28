# FROM 기반으로 할 이미지
# eclipse temurin 프로젝트 JRE 17버전
FROM eclipse-temurin:17-jre
#컨테이너를 연결할 폴더()
#TMP: 임시 디렉토리
VOLUME /tmp
# jar 실행 환경 변수 /=경로
ARG JAR_FILE=build/libs/*jar
#실제 경로에서 파일을 VOLUME 경로에 복사
COPY ${JAR_FILE} app.jar
#컨테이너가 실행될 때/구동될 때 실행하는 명령어
# commend) java -jar aaa.jar 띄어쓰기 (기본)
#main(args: Array<String>) -> 배열 = 매가변수
#문자열 배열로
#ENTRYPOINT ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]