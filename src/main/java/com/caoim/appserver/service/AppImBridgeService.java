package com.caoim.appserver.service;

import com.caoim.imcore.api.ImService;
import com.caoim.imcore.dto.MessageSendDTO;
import com.caoim.imcore.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppImBridgeService {

    @Autowired
    private ImService imService;

    public User getImUser(Long userId) {
        return imService.getUser(userId);
    }

    public Message sendPrivateMessage(Long fromId, Long toId, String content) {
        return imService.sendMessage(fromId, toId, null, content, 0);
    }

    public Message sendGroupMessage(Long fromId, Long groupId, String content) {
        return imService.sendMessage(fromId, null, groupId, content, 0);
    }

    public List<Message> getPrivateHistory(Long userId, Long targetId, int page, int size) {
        return imService.getPrivateHistory(userId, targetId, page, size);
    }

    public List<Message> getGroupHistory(Long groupId, int page, int size) {
        return imService.getGroupHistory(groupId, page, size);
    }

    public List<Conversation> getConversations(Long userId) {
        return imService.getConversations(userId);
    }

    public long getUnreadCount(Long userId) {
        return imService.getUnreadCount(userId);
    }

    public void markConversationRead(Long userId, Long conversationId) {
        imService.markAsRead(userId, conversationId);
    }

    public Group createGroup(String name, Long ownerId, List<Long> memberIds) {
        return imService.createGroup(name, ownerId, memberIds);
    }

    public List<Group> getUserGroups(Long userId) {
        return imService.getUserGroups(userId);
    }
}
