package com.project.core.battle.control.common;

import com.game.common.util.SupplyHolder;
import com.project.core.battle.BattleContext;
import com.project.core.battle.BattleStage;
import com.project.core.battle.BattleUnit;
import com.project.core.battle.BattleUtil;
import com.project.core.battle.buff.BuffUtil;
import com.project.core.battle.buff.dec.BuffDecPoint;
import com.project.core.battle.control.BattleControlId;
import com.project.core.battle.skill.BattleSkill;
import com.project.core.battle.skill.SkillEffectContextUtil;
import com.project.core.battle.skill.damage.DamageType;
import com.project.core.battle.skill.damage.SkillDamageUnit;
import com.project.core.battle.skill.effect.SkillEffectContext;
import com.project.core.battle.skill.heal.HealType;
import com.project.core.battle.skill.heal.SkillHealUnit;

public class BattleControl_RoundEnd extends BattleControl_ReadyNode {

	public BattleControl_RoundEnd(BattleControlId battleControlId) {
		super(battleControlId, BattleStage.RoundEnd);
	}

	@Override
	protected void executeReadyCommand(BattleContext battleContext) {
		handleAllBattleUnitEndRoundHealBuff(battleContext);
		handleAllBattleUnitEndRoundPoisonBuff(battleContext);
		BattleUtil.foreachBattleUnit(battleContext.getBattle(), BattleUnit::isAlive, battleUnit -> {
			BuffUtil.decBattleUnitBuffRound(battleContext, battleUnit, BuffDecPoint.EndRound);
		});
		BattleUtil.foreachBattleUnitBuffFeature(battleContext.getBattle(), BattleUnit::isAlive, feature -> {
			feature.onBattleEndRound(battleContext);
		});
		BattleUtil.foreachBattleUnit(battleContext.getBattle(), BattleUnit::isAlive, battleUnit -> {
			battleUnit.getSkillList().forEach(BattleSkill::decEndRoundCD);
		});
	}

	@Override
	protected void onSkipSuccess(BattleContext battleContext) {
		super.onSkipSuccess(battleContext);
		BattleUtil.foreachBattleUnit(battleContext.getBattle(), BattleUnit::isAlive, battleUnit -> {
			BuffUtil.decBattleUnitBuffRound(battleContext, battleUnit, BuffDecPoint.EndRound);
		});
	}

	private void handleAllBattleUnitEndRoundHealBuff(BattleContext battleContext) {
		SkillEffectContext effectContext = new SkillEffectContext();
		BattleUtil.foreachBattleUnit(battleContext.getBattle(), BattleUnit::isAlive, battleUnit -> {
			BuffUtil.foreachBuff(battleUnit, null, buff -> {
				SupplyHolder<SkillHealUnit> holder = new SupplyHolder<>(() -> HealType.Buff.createUnitHeal(battleUnit, battleUnit));
				BuffUtil.foreachBuffFeature(buff, feature -> feature.onEndRoundHeal(battleContext, holder));
				SkillHealUnit skillHealUnit = holder.getValueOnly();
				if (skillHealUnit == null){
					return;
				}
				battleContext.addBuffPlayer(buff);
				skillHealUnit.doHeal(battleContext, null, effectContext);
			});
		});
		SkillEffectContextUtil.onAllHealUnitSuccess(battleContext, effectContext);
	}

	private void handleAllBattleUnitEndRoundPoisonBuff(BattleContext battleContext) {
		SkillEffectContext effectContext = new SkillEffectContext();
		BattleUtil.foreachBattleUnit(battleContext.getBattle(), BattleUnit::isAlive, battleUnit -> {
			BuffUtil.foreachBuff(battleUnit, null, buff -> {
				SupplyHolder<SkillDamageUnit> holder = new SupplyHolder<>(() -> DamageType.Poison.createDamageUnit(battleUnit, battleUnit));
				BuffUtil.foreachBuffFeature(buff, feature -> feature.onEndRoundPoison(battleContext, holder));
				SkillDamageUnit skillDamageUnit = holder.getValueOnly();
				if (skillDamageUnit == null){
					return;
				}
				battleContext.addBuffPlayer(buff);
				skillDamageUnit.doDamage(battleContext, null, effectContext);
			});
		});
		SkillEffectContextUtil.onAllDamageUnitSuccess(battleContext, effectContext);
	}
}
