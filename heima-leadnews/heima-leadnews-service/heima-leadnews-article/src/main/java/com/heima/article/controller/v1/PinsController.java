package com.heima.article.controller.v1;

import com.heima.article.service.ApPinsService;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/pins")
@Slf4j
public class PinsController {

    @Autowired
    private ApPinsService apPinsService;

    @GetMapping("/list")
    public ResponseResult findList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Byte status) {
        return apPinsService.findList(page, size, status);
    }

    @DeleteMapping("/{id}")
    public ResponseResult deleteById(@PathVariable Long id) {
        return apPinsService.deleteById(id);
    }

    @PutMapping("/status")
    public ResponseResult updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        Byte status = Byte.parseByte(params.get("status").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;
        return apPinsService.updateStatus(id, status, reason);
    }
}
