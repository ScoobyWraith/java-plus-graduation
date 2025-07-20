package ru.practicum.ewm.common.interaction;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.ewm.common.dto.user.GetUserShortRequest;
import ru.practicum.ewm.common.dto.user.UserShortDto;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserClient {
    @PostMapping("/admin/users/short")
    Map<Long, UserShortDto> getUsersShort(GetUserShortRequest getUserShortRequest);
}
