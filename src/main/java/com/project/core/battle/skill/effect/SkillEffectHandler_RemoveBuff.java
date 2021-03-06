package com.project.core.battle.skill.effect;

import com.project.core.battle.BattleContext;
import com.project.core.battle.BattleUnit;
import com.project.core.battle.buff.Buff;
import com.project.core.battle.buff.BuffContext;
import com.project.core.battle.buff.BuffHandler;
import com.project.core.battle.buff.BuffUtil;
import com.project.core.battle.condition.ConditionType;
import com.project.core.battle.condition.IConditionContext;
import com.project.core.battle.skill.BattleSkill;
import com.project.core.battle.skill.SkillEffectUtil;
import com.project.core.battle.skill.SkillOccasion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillEffectHandler_RemoveBuff extends SkillEffectHandler {

	private static List<ConditionType> MY_CONDITIONS = Arrays.asList(ConditionType.RemoveSelfBuff, ConditionType.RemoveTargetBuff);

	private int num;
	private int generaId;

	@Override
	protected void initParam(String param) {
		String[] strings = param.split("#");
		num = Integer.parseInt(strings[0]);
		generaId = Integer.parseInt(strings[1]);
	}

	@Override
	public boolean doEffect(BattleContext battleContext, BattleUnit requestUnit, BattleSkill battleSkill, BattleUnit targetUnit, IConditionContext conditionContext, ISkillEffectContext effectContext) {
		if (num <= 0) {
			return false;
		}

		ArrayList<Buff> buffList = new ArrayList<>();
		BuffUtil.foreachBuff(targetUnit, null, buff -> {
			if (buff.buffGenera() != generaId || !buff.getTypeConfig().isCanRemove()) {
				return;
			}
			buffList.add(buff);
		});
		List<Buff> selectBuffList = battleContext.getBattle().getRandom().select(buffList, num);
		if (selectBuffList.isEmpty()) {
			return false;
		}
		for (Buff buff : selectBuffList) {
			BuffHandler.directRemoveBuff(battleContext, new BuffContext(buff, false));
		}

		List<ConditionType> myConditionTypeList = new ArrayList<>();
		if (targetUnit.getId() == requestUnit.getId()) {
			myConditionTypeList.add(ConditionType.RemoveSelfBuff);
		}
		myConditionTypeList.add(ConditionType.RemoveTargetBuff);
		for (int i = 0; i < selectBuffList.size(); i++) {
			myConditionTypeList.add(ConditionType.RemoveTargetOneBuff);
		}
		for (ConditionType conditionType : myConditionTypeList) {
			SkillEffectUtil.doAllOccasionPassiveSkillEffect(battleContext, requestUnit, targetUnit, SkillOccasion.DoSkillEffect, conditionContext, effectContext, request -> request.setConditionType(conditionType));
			if (battleSkill.isActiveSkill()) {
				SkillEffectUtil.doOneOccasionSkillEffect(battleContext, requestUnit, battleSkill, targetUnit, SkillOccasion.DoSkillEffect, null, effectContext, request -> request.setConditionType(conditionType));
			}
		}
		return true;
	}
}
