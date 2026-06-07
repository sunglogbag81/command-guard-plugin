# CommandGuard

Spigot/Paper 서버에서 서버 정보 노출 명령어와 귓속말 명령어를 차단하는 플러그인입니다.

## 차단 예시

- `/pl`, `/plugins`, `/bukkit:pl`, `/bukkit:plugins`
- `/ver`, `/version`, `/about`, `/bukkit:version`
- `/help`, `/?`, `/bukkit:help`, `/minecraft:help`
- `/w`, `/msg`, `/tell`, `/whisper`, `/r`, `/reply`
- `/minecraft:msg`, `/minecraft:tell`, `/essentials:msg` 등

## 빌드

Java 17 + Maven 필요:

```bash
cd command-guard-plugin
mvn package
```

빌드 결과:

```text
target/CommandGuard-1.0.0.jar
```

이 jar를 서버의 `plugins/` 폴더에 넣고 서버를 재시작하세요.

## 설정

서버 실행 후 생성되는 `plugins/CommandGuard/config.yml`에서 차단 목록을 수정할 수 있습니다.

설정 리로드:

```text
/commandguard reload
```

## 우회 권한

- `commandguard.bypass` — 차단 명령어 사용 가능, 기본 OP
- `commandguard.reload` — 설정 리로드 가능, 기본 OP

## 참고

현재 OpenClaw 실행 환경에는 `java`, `javac`, `mvn`이 없어 여기서 jar까지 직접 빌드하지는 못했습니다. 소스와 Maven 프로젝트는 완성되어 있습니다.
