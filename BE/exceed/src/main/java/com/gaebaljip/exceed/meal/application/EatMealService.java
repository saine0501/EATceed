package com.gaebaljip.exceed.meal.application;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.gaebaljip.exceed.food.adapter.out.FoodEntity;
import com.gaebaljip.exceed.food.application.port.out.FoodPort;
import com.gaebaljip.exceed.meal.adapter.out.MealEntity;
import com.gaebaljip.exceed.meal.adapter.out.MealFoodEntity;
import com.gaebaljip.exceed.meal.application.port.in.EatMealCommand;
import com.gaebaljip.exceed.meal.application.port.in.EatMealUsecase;
import com.gaebaljip.exceed.meal.application.port.out.MealFoodPort;
import com.gaebaljip.exceed.meal.application.port.out.MealPort;
import com.gaebaljip.exceed.meal.exception.InvalidGException;
import com.gaebaljip.exceed.meal.exception.InvalidMultipleAndGException;
import com.gaebaljip.exceed.meal.exception.InvalidMultipleException;
import com.gaebaljip.exceed.member.adapter.out.persistence.MemberEntity;
import com.gaebaljip.exceed.member.application.port.out.MemberPort;

import lombok.RequiredArgsConstructor;

/**
 * 식사를 등록한다.
 *
 * @author hwangdaesun
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class EatMealService implements EatMealUsecase {

    private final FoodPort foodPort;
    private final MemberPort memberPort;
    private final MealPort mealPort;
    private final MealFoodPort mealFoodPort;

    /**
     * 몇 인분(multiple) 검증 식사를 등록한다.
     *
     * @param command : 누가 무엇을 언제 얼마나 먹었는 지에 대한 정보가 들어있다.
     * @return mealId : 식사 엔티티의 PK
     * @throws InvalidMultipleException : 0인분 이하거나 100인분 초과일 경우
     */
    @Override
    @Transactional
    public Long execute(EatMealCommand command) {
        validateGAndMultiple(command);
        List<FoodEntity> foodEntities =
                foodPort.queryAllEntities(
                        command.eatMealFoodDTOS().stream()
                                .mapToLong(eatMealFood -> eatMealFood.foodId())
                                .boxed()
                                .toList());
        MemberEntity memberEntity = memberPort.query(command.memberId());
        MealEntity mealEntity =
                mealPort.command(MealEntity.createMeal(memberEntity, command.mealType()));
        mealFoodPort.command(
                MealFoodEntity.createMealFoods(
                        foodEntities, mealEntity, command.eatMealFoodDTOS()));
        return mealEntity.getId();
    }

    private void validateGAndMultiple(EatMealCommand command) {
        command.eatMealFoodDTOS()
                .forEach(
                        dto -> {
                            if ((dto.multiple() != null && dto.g() != null)
                                    || (dto.multiple() == null && dto.g() == null)) {
                                throw InvalidMultipleAndGException.EXCEPTION;
                            }
                            if (dto.multiple() != null) {
                                validateMultiple(dto.multiple());
                            }
                            if (dto.g() != null) {
                                validateG(dto.g());
                            }
                        });
    }

    private void validateMultiple(double multiple) {
        if (multiple <= 0 || multiple > 100) {
            throw InvalidMultipleException.EXECPTION;
        }
    }

    private void validateG(int g) {
        if (g <= 0) {
            throw InvalidGException.EXCEPTION;
        }
    }
}
