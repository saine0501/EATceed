package com.gaebaljip.exceed.application.service.food;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaebaljip.exceed.adapter.out.jpa.food.FoodEntity;
import com.gaebaljip.exceed.adapter.out.redis.RedisUtils;
import com.gaebaljip.exceed.application.port.in.food.GetFoodQuery;
import com.gaebaljip.exceed.application.port.out.food.FoodPort;
import com.gaebaljip.exceed.common.EatCeedStaticMessage;
import com.gaebaljip.exceed.dto.response.GetFoodResponse;
import com.gaebaljip.exceed.dto.response.GetFoodsAutoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFoodService implements GetFoodQuery {
    private final String postfix = ";";
    private final FoodPort loadFoodPort;
    private final RedisUtils redisUtils;

    @Override
    @Transactional(readOnly = true)
    public GetFoodsAutoResponse execute(String prefix) {
        Set<Object> autoComplete = getAutoComplete(prefix);
        List<String> autoCompleteList =
                autoComplete.stream()
                        .filter(o -> checkPrefixAndPostfix(o, prefix, postfix))
                        .map(o -> (String) o)
                        .limit(10)
                        .toList();
        return GetFoodsAutoResponse.from(autoCompleteList);
    }

    private Set<Object> getAutoComplete(String prefix) {
        Long index = redisUtils.zRank(EatCeedStaticMessage.REDIS_AUTO_COMPLETE_KEY, prefix);
        return redisUtils.zRange(EatCeedStaticMessage.REDIS_AUTO_COMPLETE_KEY, index, index + 10);
    }

    private boolean checkPrefixAndPostfix(Object o, String prefix, String postfix) {
        return o instanceof String string && string.startsWith(prefix) && string.endsWith(postfix);
    }

    @Override
    public GetFoodResponse execute(Long foodId) {
        FoodEntity foodEntity = loadFoodPort.query(foodId);
        return GetFoodResponse.of(foodEntity);
    }
}
