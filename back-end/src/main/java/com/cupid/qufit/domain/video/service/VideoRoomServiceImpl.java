package com.cupid.qufit.domain.video.service;

import com.cupid.qufit.domain.member.repository.profiles.MemberRepository;
import com.cupid.qufit.domain.video.dto.VideoRoomRequest;
import com.cupid.qufit.domain.video.dto.VideoRoomResponse;
import com.cupid.qufit.domain.video.repository.VideoRoomParticipantRepository;
import com.cupid.qufit.domain.video.repository.VideoRoomRepository;
import com.cupid.qufit.entity.Member;
import com.cupid.qufit.entity.video.VideoRoom;
import com.cupid.qufit.entity.video.VideoRoomParticipant;
import com.cupid.qufit.entity.video.VideoRoomStatus;
import com.cupid.qufit.global.exception.ErrorCode;
import com.cupid.qufit.global.exception.exceptionType.MemberException;
import com.cupid.qufit.global.exception.exceptionType.VideoException;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class VideoRoomServiceImpl implements VideoRoomService {

    private final VideoRoomRepository videoRoomRepository;
    private final MemberRepository memberRepository;
    private final VideoRoomParticipantRepository videoRoomParticipantRepository;

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    /**
     * 방 생성
     */
    @Override
    public VideoRoomResponse createVideoRoom(VideoRoomRequest videoRoomRequest) {
        // ! 1. 입력받은 방 제목, 방 인원 수, 태그를 통해 방 생성 및 DB 저장
        VideoRoom videoRoom = VideoRoomRequest.to(videoRoomRequest);
        videoRoomRepository.save(videoRoom);

        // ! 2. 본인 참가를 위한 joinVideoRoom 을 통해 토큰 생성
        String token = joinVideoRoom(videoRoom.getVideoRoomId(), videoRoomRequest.getParticipantId());
        return VideoRoomResponse.from(videoRoom, token);
    }

    /**
     * 방 참가
     */
    @Override
    public String joinVideoRoom(Long videoRoomId, Long participantId) {
        // ! 1. 방 찾기
        VideoRoom videoRoom = videoRoomRepository.findById(videoRoomId)
                                                 .orElseThrow(() -> new VideoException(ErrorCode.VIDEO_ROOM_NOT_FOUND));

        // ! 2. 멤버 찾기
        Member member = memberRepository.findById(participantId)
                                        .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        // ! 3. 참가자 생성
        VideoRoomParticipant newParticipant = VideoRoomParticipant.builder()
                                                                  .videoRoom(videoRoom)
                                                                  .joinedAt(LocalDateTime.now())
                                                                  .member(member)
                                                                  .build();

        // ! 4. 방에 참가자 추가
        videoRoom.getParticipants().add(newParticipant);

        // ! 5. 방 정보 업데이트
        if (newParticipant.getMember().getGender() == 'm') {
            videoRoom.setCurMCount(videoRoom.getCurMCount() + 1);
        } else if (newParticipant.getMember().getGender() == 'f') {
            videoRoom.setCurMCount(videoRoom.getCurWCount() + 1);
        }
        videoRoomRepository.save(videoRoom);

        // ! 6. 토큰 생성
        AccessToken token = new AccessToken(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        token.setName(participantId.toString());
        token.setIdentity(participantId.toString());
        token.addGrants(new RoomJoin(true), new RoomName(videoRoomId.toString()));

        return token.toJwt();
    }

    /**
     * 방 업데이트
     */
    @Override
    public VideoRoomResponse updateVideoRoom(Long videoRoomId, VideoRoomRequest videoRoomRequest) {
        // ! 1. 방 찾기
        VideoRoom videoRoom = videoRoomRepository.findById(videoRoomId)
                                                 .orElseThrow(() -> new VideoException(ErrorCode.VIDEO_ROOM_NOT_FOUND));

        // ! 2. 방 정보 업데이트 (방 제목, 최대 인원 수, 취미, 성격 태그)
        videoRoom.setVideoRoomName(videoRoomRequest.getVideoRoomName());
        videoRoom.setMaxParticipants(videoRoomRequest.getMaxParticipants());
        videoRoom.setVideoRoomHobby(VideoRoomRequest.toHobbyList(videoRoomRequest, videoRoom));
        videoRoom.setVideoRoomPersonality(VideoRoomRequest.toPersonalityList(videoRoomRequest, videoRoom));
        videoRoomRepository.save(videoRoom);

        return VideoRoomResponse.from(videoRoom, null);
    }

    /**
     * 방 삭제
     */
    @Override
    public void deleteVideoRoom(Long videoRoomId) {
        // ! 1. 방 찾기
        VideoRoom videoRoom = videoRoomRepository.findById(videoRoomId)
                                                 .orElseThrow(() -> new VideoException(ErrorCode.VIDEO_ROOM_NOT_FOUND));
        // ! 2. 방 제거
        videoRoomRepository.delete(videoRoom);
    }

    /**
     * 방 떠나기
     */
    @Override
    public void leaveVideoRoom(Long videoRoomId, Long participantId) {
        // ! 1. 방 찾기
        VideoRoom videoRoom = videoRoomRepository.findById(videoRoomId)
                                                 .orElseThrow(() -> new VideoException(ErrorCode.VIDEO_ROOM_NOT_FOUND));

        // ! 2. 방 인원 1명일 경우 방 삭제
        if (videoRoom.getCurMCount() + videoRoom.getCurWCount() == 1) {
            videoRoomRepository.delete(videoRoom);
            return;
        }

        // ! 3. 참가자 찾기
        VideoRoomParticipant participant = videoRoomParticipantRepository.findById(participantId)
                                                                         .orElseThrow(
                                                                                 () -> new VideoException(
                                                                                         ErrorCode.PARTICIPANT_NOT_FOUND));
        // ! 4. 방에서 참가자 제거
        videoRoom.getParticipants().remove(participant);

        // ! 5. 방 현재 인원 수 업데이트
        if (participant.getMember().getGender() == 'm') {
            videoRoom.setCurMCount(videoRoom.getCurMCount() - 1);
        } else if (participant.getMember().getGender() == 'f') {
            videoRoom.setCurWCount(videoRoom.getCurWCount() - 1);
        }
        videoRoomRepository.save(videoRoom);
    }

    /**
     * 방 상세 정보 조회
     */
    @Override
    public VideoRoomResponse getVideoRoomDetail(Long videoRoomId) {
        // ! 1. 방 찾기
        VideoRoom videoRoom = videoRoomRepository.findById(videoRoomId)
                                                 .orElseThrow(() -> new VideoException(ErrorCode.VIDEO_ROOM_NOT_FOUND));

        // ! 2. 방 참가자의 태그를 포함한 반환
        return VideoRoomResponse.withDetails(videoRoom);
    }

    /**
     * 방 리스트 조회(최신순)
     */
    @Override
    public Map<String, Object> getVideoRoomList(Pageable pageable) {
        // ! 1. 대기방 리스트 최신순 조회
        Page<VideoRoom> videoRoomPage = videoRoomRepository.findByStatus(VideoRoomStatus.READY, pageable);

        List<VideoRoomResponse> videoRoomResponses = videoRoomPage.stream()
                                                                  .map(VideoRoomResponse::toBasicResponse)
                                                                  .collect(Collectors.toList());
        // ! 2. 응답용 리스트 데이터 가공
        Map<String, Object> response = new HashMap<>();
        response.put("videoRoomList", videoRoomResponses);
        response.put("page", Map.of(
                "totalElements", videoRoomPage.getTotalElements(),
                "totalPages", videoRoomPage.getTotalPages(),
                "currentPage", videoRoomPage.getNumber(),
                "pageSize", videoRoomPage.getSize()
        ));
        return response;
    }
}