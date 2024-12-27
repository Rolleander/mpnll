package com.broll.mpnll.server.user;

import com.google.protobuf.Any;

public interface UserSettingsBuilder {
    
    Any build(User user);
}
