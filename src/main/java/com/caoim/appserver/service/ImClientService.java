package com.caoim.appserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caoim.imcore.api.ImService;
import com.caoim.imcore.client.ImFeignClient;
import com.caoim.imcore.common.Result;
import com.caoim.imcore.dto.GroupCreateDTO;
import com.caoim.imcore.dto.MessageSendDTO;
import com.caoim.imcore.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImClientService implements ImService {

    private final ImFeignClient imFeignClient;

    @Override
    public User getUser(Long userId) {
        Result<User> result = imFeignClient.getUserInfo(userId);
        return result.getData();
    }

    @Override
    public User findByUsername(String username) {
        throw new UnsupportedOperationException("远程调用不支持按用户名查找，请使用 getUser(userId)");
    }

    @Override
    public Message sendMessage(Long fromId, Long toId, Long groupId, String content, Integer msgType) {
        MessageSendDTO dto = new MessageSendDTO();
        dto.setFromId(fromId);
        dto.setToId(toId);
        dto.setGroupId(groupId);
        dto.setContent(content);
        dto.setMsgType(msgType);

        Result<Message> result = imFeignClient.sendMessage(dto);
        return result.getData();
    }

    @Override
    public List<Message> getPrivateHistory(Long userId, Long targetId, int page, int size) {
        Result<Page<Message>> result = imFeignClient.getPrivateMessages(userId, targetId, page, size);
        return result.getData().getRecords();
    }

    @Override
    public List<Message> getGroupHistory(Long groupId, int page, int size) {
        Result<Page<Message>> result = imFeignClient.getGroupMessages(groupId, page, size);
        return result.getData().getRecords();
    }

    @Override
    public void markAsRead(Long userId, Long conversationId) {
        imFeignClient.clearUnreadCount(userId, conversationId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        Result<Long> result = imFeignClient.getUnreadCount(userId);
        return result.getData();
    }

    @Override
    public List<Conversation> getConversations(Long userId) {
        Result<List<Conversation>> result = imFeignClient.getConversations(userId);
        return result.getData();
    }

    @Override
    public void clearUnread(Long conversationId) {
        throw new UnsupportedOperationException("请使用 markAsRead(userId, conversationId)");
    }

    @Override
    public void deleteConversation(Long userId, Long conversationId) {
        imFeignClient.deleteConversation(userId, conversationId);
    }

    @Override
    public Group createGroup(String name, Long ownerId, List<Long> memberIds) {
        GroupCreateDTO dto = new GroupCreateDTO();
        dto.setName(name);
        dto.setMemberIds(memberIds);

        Result<Group> result = imFeignClient.createGroup(dto);
        return result.getData();
    }

    @Override
    public List<Group> getUserGroups(Long userId) {
        Result<List<Group>> result = imFeignClient.getUserGroups(userId);
        return result.getData();
    }

    @Override
    public void addGroupMembers(Long groupId, List<Long> userIds) {
        for (Long userId : userIds) {
            imFeignClient.sendFriendRequest(groupId, userId);
        }
    }

    @Override
    public void removeGroupMember(Long groupId, Long userId) {
        imFeignClient.deleteFriend(groupId, userId);
    }

    @Override
    public void sendFriendRequest(Long userId, Long friendId) {
        imFeignClient.sendFriendRequest(userId, friendId);
    }

    @Override
    public void acceptFriendRequest(Long userId, Long friendId) {
        imFeignClient.acceptFriendRequest(userId, friendId);
    }

    @Override
    public void rejectFriendRequest(Long userId, Long friendId) {
        imFeignClient.rejectFriendRequest(userId, friendId);
    }

    @Override
    public List<Friend> getFriends(Long userId) {
        Result<List<Friend>> result = imFeignClient.getFriends(userId);
        return result.getData();
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        imFeignClient.deleteFriend(userId, friendId);
    }
}
