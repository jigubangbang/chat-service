package com.jigubangbang.chat_service.mapper;

import com.jigubangbang.chat_service.model.ChatGroupDto;
import com.jigubangbang.chat_service.model.ChatMsgDto;
import com.jigubangbang.chat_service.model.ChatRoomDto;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMapper {

    // 채팅방 입장
    ChatRoomDto getChatRoomInfo(Long chatId);
    ChatGroupDto getChatGroupMemberWithNickname(Long chatId, String userId);
    ChatGroupDto getChatGroupMember(Long chatId, String userId);
    void insertGroupMember(ChatGroupDto dto);
    
    // 채팅 메세지 조회 및 24시간 이내 메세지 조회
    List<ChatMsgDto> getChatMessages(Long chatId);
    List<ChatMsgDto> getRecChatMessages(Long chatId, LocalDateTime since);
    
    // 현 채팅방 멤버 조회
    List<ChatGroupDto> getChatGroupMembers(Long chatId);
    
    // 최초 생성자 조회
    List<ChatGroupDto> getChatGroupCreators(Long chatId);
    
    // 채팅 메세지 전송
    void insertMessage(ChatMsgDto dto);

    // 채팅 참여자 강제 탈퇴
    void removeGroupMemberByCreator(Map<String, Object> param);

    // 채팅방 나가기
    void leaveGroupMemberByUser(Long chatId, String userId);

    // 회원 운영진 승격
    void promoteToAdmin(String groupType, int groupId, String userId);

    // 운영진 제외
    void demoteAdmin(String groupType, int groupId, String userId);

    // 채팅방 정보 수정
    int saveDescription(ChatRoomDto chatRoomDto);

}
 