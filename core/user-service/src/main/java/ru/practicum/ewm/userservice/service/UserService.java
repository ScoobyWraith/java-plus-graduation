package ru.practicum.ewm.userservice.service;

import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.ewm.common.dto.user.GetUserShortRequest;
import ru.practicum.ewm.common.dto.user.UserShortDto;
import ru.practicum.ewm.userservice.dto.NewUserRequestDto;
import ru.practicum.ewm.userservice.dto.UserDto;
import ru.practicum.ewm.userservice.params.UserQueryParams;

import java.util.List;
import java.util.Map;

public interface UserService {

    UserDto createUser(NewUserRequestDto newUserRequestDto);

    List<UserDto> getAllUsers(UserQueryParams params);

    UserDto getUserById(Long userId);

    void deleteUser(Long userId);

    Map<Long, UserShortDto> getUsersShort(@RequestBody GetUserShortRequest getUserShortRequest);
}
