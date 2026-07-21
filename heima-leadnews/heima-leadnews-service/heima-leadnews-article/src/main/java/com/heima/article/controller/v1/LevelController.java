package com.heima.article.controller.v1;

import com.heima.article.service.LevelService;
import com.heima.model.article.pojos.ApUserLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/level")
public class LevelController {

    @Autowired
    private LevelService levelService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApUserLevel> getUserLevel(@PathVariable Long userId) {
        ApUserLevel userLevel = levelService.getUserLevel(userId);
        return ResponseEntity.ok(userLevel);
    }

    @GetMapping("/user/{userId}/info")
    public ResponseEntity<Map<String, Object>> getUserLevelInfo(@PathVariable Long userId) {
        Map<String, Object> levelInfo = levelService.getUserLevelInfo(userId);
        return ResponseEntity.ok(levelInfo);
    }

    @GetMapping("/user/{userId}/tasks")
    public ResponseEntity<Map<String, Object>> getTodayTaskProgress(@PathVariable Long userId) {
        Map<String, Object> taskProgress = levelService.getTodayTaskProgress(userId);
        return ResponseEntity.ok(taskProgress);
    }

    @GetMapping("/user/{userId}/permissions")
    public ResponseEntity<List<String>> getUserPermissions(@PathVariable Long userId) {
        List<String> permissions = levelService.getUserPermissions(userId);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/user/{userId}/permission/{permissionCode}")
    public ResponseEntity<Map<String, Boolean>> checkPermission(
            @PathVariable Long userId,
            @PathVariable String permissionCode) {
        boolean hasPermission = levelService.hasPermission(userId, permissionCode);
        return ResponseEntity.ok(Map.of("hasPermission", hasPermission));
    }

    @PostMapping("/action")
    public ResponseEntity<Void> recordAction(
            @RequestParam Long userId,
            @RequestParam String actionType,
            @RequestParam(required = false) String actionDetail) {
        levelService.recordAction(userId, actionType, actionDetail);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-in")
    public ResponseEntity<Map<String, Object>> checkIn(@RequestParam Long userId) {
        Map<String, Object> result = levelService.checkIn(userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/action/with-limit")
    public ResponseEntity<Map<String, Object>> recordActionWithLimit(
            @RequestParam Long userId,
            @RequestParam String actionType,
            @RequestParam(required = false) String actionDetail) {
        Map<String, Object> result = levelService.recordActionWithLimit(userId, actionType, actionDetail);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/power")
    public ResponseEntity<Map<String, Object>> calculatePower(
            @RequestParam Long userId,
            @RequestParam Long articleId,
            @RequestParam String changeType,
            @RequestParam Integer powerChange) {
        Map<String, Object> result = levelService.calculatePowerWithLimit(userId, articleId, changeType, powerChange);
        return ResponseEntity.ok(result);
    }
}
