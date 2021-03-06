package com.project.core.battle.ai;

import com.game.common.util.RandomUtil;
import com.project.core.battle.BattleContext;
import com.project.core.battle.BattleUnit;
import com.project.core.battle.operate.OperateSkill;
import com.project.core.battle.skill.BattleSkill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BattleAIService {

	private static final Logger logger = LoggerFactory.getLogger(BattleAIService.class);

	private static BattleAIService instance = new BattleAIService();

	public static BattleAIService getInstance() {
		return instance;
	}

	public OperateSkill randomOperate(BattleContext battleContext, BattleUnit battleUnit){
		BattleSkill battleSkill = RandomUtil.select(battleUnit.getSkillList());
		OperateSkill operate = new OperateSkill(battleUnit.getId(), 0, battleSkill.getSkillId());
		return operate;
	}
}
