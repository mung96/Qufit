import { useSetRoomIdStore } from '@stores/video/roomStore';
import useRoom from '@hooks/useRoom';
import ParticipantVideo from '@components/video/ParticipantVideo';
import { useEffect, useRef, useState } from 'react';
import { useProblemsStore, useSetProblemsStore, useSetResultsStore } from '@stores/video/gameStore';
import Loading from '@components/game/\bstep/Loading';
import BalanceGame from '@components/game/\bstep/BalanceGame';
import GameResult from '@components/game/\bstep/GameResult';
import GamePlay from '@components/game/\bstep/GamePlay';
import GameEnd from '@components/game/\bstep/GameEnd';
import * as StompJs from '@stomp/stompjs';
import { useParams } from 'react-router-dom';
import { qufitAcessTokenA, qufitAcessTokenB, qufitAcessTokenC, qufitAcessTokenD } from '@apis/axios';
import GameIntro from '@components/game/\bstep/GameIntro';
import useTimer from '@hooks/useTimer';

export let accessToken = '';
if (location.port === '3000') {
    accessToken = qufitAcessTokenA;
} else if (location.port === '3001') {
    accessToken = qufitAcessTokenB;
} else if (location.port === '3002') {
    accessToken = qufitAcessTokenC;
} else if (location.port === '3003') {
    accessToken = qufitAcessTokenD;
}
type RoomStep =
    | 'wait'
    | 'active'
    | 'loading'
    | 'game'
    | 'play'
    | 'result'
    | 'resultLoading1'
    | 'resultLoading2'
    | 'resultLoading3'
    | 'end';
type beforeResult = {
    balanceGameChoiceId: number;
    balanceGameId: number;
    choiceContent: string;
    choiceNum: number;
    memberId: number;
    videoRoomId: number;
};
function GroupVideoPage() {
    const roomMax = 4;
    const [isMeetingStart, setIsMettingStart] = useState(false);
    const { videoRoomId } = useParams();
    const [roomStep, setRoomStep] = useState<RoomStep>('end');
    const { createRoom, joinRoom, leaveRoom } = useRoom();
    const [gameStage, setGameStage] = useState(0);
    const roomId = 127;
    const setRoomId = useSetRoomIdStore();
    const restSec = useTimer(15 * 60, () => {});

    const setResults = useSetResultsStore();
    const problems = useProblemsStore();
    const setProblems = useSetProblemsStore();
    const client = useRef<StompJs.Client | null>(null);

    const { isHost } = useRoom();
    const publishSocket = (data: any) => {
        client.current?.publish({
            destination: `/pub/game/${roomId}`,
            body: JSON.stringify(data),
        });
    };

    const startMeeting = () => {
        publishSocket({
            isRoomStart: true,
        });
        setIsMettingStart(true);
    };

    const startGame = () => {
        publishSocket({
            isGameStart: true,
        });
        setRoomStep('loading');
    };

    const startPlay = () => {
        publishSocket({
            isChoiceStart: true,
        });
        setGameStage((prev) => prev + 1);
        setRoomStep('play');
    };

    const endChoice = (choice: any) => {
        publishSocket(choice);
    };

    const afterSubscribe = (response: any, message: string, func: any) => {
        if (response.message === message) {
            func();
        }
    };
    const connect = () => {
        try {
            client.current = new StompJs.Client({
                brokerURL: `ws://i11a209.p.ssafy.io:8080/stomp/chat`,
                connectHeaders: {
                    Authorization: `Bearer ${accessToken}`,
                },
                debug: function (str) {
                    console.log('소켓 디버그:', str);
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
                onConnect: () => {
                    client.current?.subscribe(`/sub/game/${roomId}`, (message) => {
                        const response = JSON.parse(message.body);

                        afterSubscribe(response, '미팅룸 시작을 성공했습니다.', () => {
                            setIsMettingStart(true);
                        });
                        afterSubscribe(response, '게임 시작을 성공했습니다.', () => {
                            setRoomStep('loading');
                            setProblems(response.result);
                        });
                        afterSubscribe(response, '선택지 선택을 시작했습니다.', () => {
                            setRoomStep('play');
                            {
                                !isHost && setGameStage((prev) => prev + 1);
                            }
                        });
                        afterSubscribe(response, '선택을 완료했습니다.', () => {
                            console.log(response);
                        });

                        afterSubscribe(response, '게임 결과를 조회했습니다.', () => {
                            const processedResult = response.result.reduce((acc, result: beforeResult) => {
                                if (!acc[result.balanceGameId]) {
                                    acc[result.balanceGameId] = {};
                                }
                                acc[result.balanceGameId][result.memberId] = result.choiceNum;
                                return acc;
                            }, {});
                            setResults(processedResult);
                        });
                    });
                },
            });

            if (client.current) {
                client.current.activate();
            } else {
                console.log('클라이언트가 초기화되지 않았습니다.');
            }
        } catch (err) {
            console.log(err);
        }
    };
    // 연결 끊기
    const disConnect = () => {
        if (client.current === null) {
            return;
        }
        client.current.deactivate();
    };
    useEffect(() => {
        setRoomId(roomId); //나중에 param에서 따와야함
        connect();
        return () => disConnect();
    }, []);
    return (
        <>
            <div className="flex flex-col justify-center w-full h-screen ">
                <ParticipantVideo roomMax={roomMax} gender="m" />
                <div className="flex flex-col items-center justify-center py-4">
                    <div className="flex flex-col gap-4">
                        <button onClick={createRoom}>생성하기</button>

                        <button
                            onClick={() => {
                                joinRoom(roomId);
                            }}
                        >
                            입장하기
                        </button>
                        <button onClick={() => leaveRoom(roomId)}>나가기</button>
                    </div>

                    {roomStep === 'active' && <GameIntro onNext={startGame} />}
                    {roomStep === 'loading' && <Loading onNext={() => setRoomStep('game')} />}
                    {roomStep === 'game' && <BalanceGame onNext={startPlay} />}
                    {roomStep === 'play' && (
                        <GamePlay
                            id={problems[gameStage].balanceGameId}
                            title={problems[gameStage].content}
                            scenario1={problems[gameStage].scenario1}
                            scenario2={problems[gameStage].scenario2}
                            onNext={(choice: any) => {
                                endChoice(choice);
                                setRoomStep('resultLoading1');
                            }}
                        />
                    )}
                    {roomStep === 'resultLoading1' && (
                        <Loading
                            onNext={() => {
                                isHost &&
                                    publishSocket({
                                        getResult: true,
                                    });
                                setRoomStep('resultLoading2');
                            }}
                        />
                    )}
                    {roomStep === 'resultLoading2' && (
                        <Loading
                            onNext={() => {
                                setRoomStep('result');
                            }}
                        />
                    )}

                    {roomStep === 'result' && (
                        <GameResult
                            id={problems[gameStage].balanceGameId}
                            title={problems[gameStage].content}
                            scenario1={problems[gameStage].scenario1}
                            scenario2={problems[gameStage].scenario2}
                            gameStage={gameStage}
                            onStop={() => setRoomStep('end')}
                            onNext={startPlay}
                        />
                    )}
                    {roomStep === 'end' && <GameEnd restSec={restSec} />}
                </div>
                <ParticipantVideo roomMax={roomMax} gender="f" />
            </div>
        </>
    );
}

export default GroupVideoPage;

{
    /* <div className="hidden">
                    {participants.map((participant) => (
                        <AudioComponent
                            key={participant.nickname}
                            track={
                                participant.info.audioTrackPublications.values().next().value?.audioTrack || undefined
                            }
                        />
                    ))}
                </div> */
}
