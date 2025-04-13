# 🎭 QUFIT - 블라인드 로테이션 소개팅

<div align="center">

> **블라인드 미팅 서비스**  
> **개발기간: 2024.07 ~ 2024.08 (6주)**

</div>

# 🛠️ 기술 스택

#### 🖼️ Frontend

![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Zustand](https://img.shields.io/badge/Zustand-000000?style=for-the-badge&logo=react&logoColor=white)
![React Query](https://img.shields.io/badge/React_Query-FF4154?style=for-the-badge&logo=react-query&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)

### 🤝 Communication

![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitLab](https://img.shields.io/badge/GitLab-FCA121?style=for-the-badge&logo=gitlab&logoColor=white)
![Jira](https://img.shields.io/badge/Jira-0052CC?style=for-the-badge&logo=jira&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![Mattermost](https://img.shields.io/badge/Mattermost-0058CC?style=for-the-badge&logo=mattermost&logoColor=white)

# 💪 맡은 역할

## ⭐️ 1. 회원가입 페이지

| 회원가입 페이지                                                                                              | 회원가입 페이지2                                                                                                                              |
| :----------------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------- |
| <img width="500px" src="https://i.postimg.cc/k4vK835Y/image.jpg" />                                          | <img width="500px" src="https://i.postimg.cc/65mRCh9K/2.jpg" />                                                                               |
| - 본인의 정보(mbti, 성격, 취미 등)를 수집합니다.<br> - 이상형의 정보(나이차, mbti, 성격, 취미)을 수집합니다. | - [시작하기] 버튼으로 회원가입을 완료합니다. <br> - 회원가입을 완료한 회원은 '가입 대기 중' 회원입니다. 관리자가 승인 후 로그인이 가능합니다. |

### 💡 구현 방식

#### ✅ 지역 상태 관리를 활용하여 Funnel 구현

#### ✅ Form에서 재사용 가능한 useForm 커스텀 훅 구현

➡️ [회원가입 페이지](https://github.com/mung96/Qufit/blob/front-dev/front-end/src/pages/SignupPage.tsx) 코드 보기  
➡️ [useForm](https://github.com/mung96/Qufit/blob/front-dev/front-end/src/hooks/useForm.tsx) 코드 보기

## ⭐️ 2. 마이페이지

| 마이페이지(회원 정보) 페이지1                                                                                                                                                     | 마이페이지(이상형 정보) 페이지                                         |
| :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------------------- |
| <img width="500px" src="https://i.postimg.cc/D0NB8J5P/IMG-8527.jpg" />                                                                                                            | <img width="500px" src="https://i.postimg.cc/ydvfCrZ5/IMG-8526.jpg" /> |
| - 회원 정보(mbti, 성격, 취미)를 확인합니다.<br> - [프로필 수정]버튼으로 정보를 수정합니다.<br> - [AI에게 프로필 생성받기]버튼으로 내 정보에 맞는 동물 프로필 사진을 추천받습니다. | - 이상형 정보(나이차, mbti, 성격, 취미)를 확인/수정합니다.             |

### 💡 구현 방식

#### ✅ 단일 선택은 radio, 다중 선택은 checkbox type을 활용하여 웹 접근성을 지켰습니다.

#### ✅ context API를 활용하여 재사용가능한 Radio, Checkbox를 구현했습니다.

➡️ [마이페이지](https://github.com/mung96/Qufit/blob/front-dev/front-end/src/pages/Mypage.tsx) 코드 보기  
➡️ [Radio 컴포넌트](https://github.com/mung96/Qufit/blob/front-dev/front-end/src/components/common/radio/Radio.tsx) 코드 보기

## ⭐️ 3. 미팅방 페이지

| 다대다 화상 미팅 페이지                                                                                                                                                                    | 다대다 화상 미팅 종료 페이지                                           |
| :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :--------------------------------------------------------------------- |
| <img width="500px" src="https://i.postimg.cc/jjN0KKx1/IMG-8518.jpg" />                                                                                                                     | <img width="500px" src="https://i.postimg.cc/Fsn2L11q/IMG-8521.jpg" /> |
| - 위 화면은 남성, 아래 화면은 여성 참여자의 화면이 보입니다. <br> - 방장은 왕관 스티커로 구분합니다. <br> - `[start]` 버튼으로 미팅을 시작합니다. <br> - 방장만 미팅을 시작할 수 있습니다. | - `[이동하기]`버튼으로 1:1 미팅방으로 이동합니다.                      |

| 밸런스 게임 시작 페이지                                                | 밸런스 게임 선택 페이지                                                |
| :--------------------------------------------------------------------- | :--------------------------------------------------------------------- |
| <img width="500px" src="https://i.postimg.cc/ZRFGBcyF/IMG-8519.jpg" /> | <img width="500px" src="https://i.postimg.cc/C5hWjqGy/IMG-8520.jpg" /> |
| - `[CLICK START]`버튼으로 밸런스 게임을 시작합니다.                    | - 게임 설명을 보고, 선택지를 각자 선택합니다.                          |

| 일대일 화상 미팅 페이지                                                                 | 일대일 화상 미팅 종료 페이지                                           |
| :-------------------------------------------------------------------------------------- | :--------------------------------------------------------------------- |
| <img width="500px" src="https://i.postimg.cc/0jyB6qvP/IMG-8522.png" />                  | <img width="500px" src="https://i.postimg.cc/Xv9hm8xS/IMG-8523.jpg" /> |
| - 화면 상단에 남은 시간을 확인합니다. <br> - 화면 하단에 밸런스 게임 결과를 확인합니다. | - 상대방과 매칭되었는지 확인합니다.                                    |

### 💡 구현 방식

#### ✅ 시그널링 서버, TURN/STUN 서버, 미디어 서버를 한 번에 제공해주는 Live-Kit 라이브러리를 사용

#### ✅ 사용자들이 실시간으로 참여하는 밸런스 게임은 WebSocket을 활용해 구현했습니다.

➡️ [단체 미팅방](https://github.com/mung96/Qufit/blob/front-dev/front-end/src/pages/GroupVideoPage.tsx) 코드 보기
