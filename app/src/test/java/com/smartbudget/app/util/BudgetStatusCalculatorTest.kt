package com.smartbudget.app.util
import org.junit.Assert.assertEquals
import org.junit.Test
class BudgetStatusCalculatorTest {
    @Test
        fun belowEightyPercent_isWithinBudget() {
            assertEquals(BudgetStatus.WITHIN_BUDGET, BudgetStatusCalculator.calculate(100.0, 79.99))
        }

    @Test
        fun exactlyEightyPercent_isCloseToExceeding() {
            assertEquals(BudgetStatus.CLOSE_TO_EXCEEDING, BudgetStatusCalculator.calculate(100.0, 80.0))
        }

    @Test
        fun exactlyOneHundredPercent_isCloseToExceeding() {
            assertEquals(BudgetStatus.CLOSE_TO_EXCEEDING, BudgetStatusCalculator.calculate(100.0, 100.0))
        }

    @Test
        fun aboveOneHundredPercent_isOverBudget() {
            assertEquals(BudgetStatus.OVER_BUDGET, BudgetStatusCalculator.calculate(100.0, 100.01))
        }

    @Test
        fun zeroSpent_isWithinBudget() {
            assertEquals(BudgetStatus.WITHIN_BUDGET, BudgetStatusCalculator.calculate(500.0, 0.0))
        }

    @Test
        fun zeroBudgetWithSpending_isOverBudget() {
            assertEquals(BudgetStatus.OVER_BUDGET, BudgetStatusCalculator.calculate(0.0, 10.0))
        }

    @Test
        fun zeroBudgetNoSpending_isWithinBudget() {
            assertEquals(BudgetStatus.WITHIN_BUDGET, BudgetStatusCalculator.calculate(0.0, 0.0))
        }
    }
