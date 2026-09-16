import { ChevronLeft, ChevronRight } from "lucide-react";
import {
	addMonths,
	currentMonthKey,
	formatMonthLabel,
	type MonthKey,
} from "#/lib/month";

type MonthNavProps = {
	month: MonthKey;
	onChange: (month: MonthKey) => void;
};

export function MonthNav({ month, onChange }: MonthNavProps) {
	const isCurrent = month === currentMonthKey();

	return (
		<div className="flex items-center gap-1">
			<button
				type="button"
				aria-label="Mês anterior"
				onClick={() => onChange(addMonths(month, -1))}
				className="grid size-9 place-items-center rounded-full border border-line bg-[var(--chip-bg)] text-ink-soft hover:text-ink hover:border-lagoon-deep/40"
			>
				<ChevronLeft className="size-4" />
			</button>

			<div className="min-w-[11.5rem] text-center">
				<span className="font-display text-lg font-semibold text-ink">
					{formatMonthLabel(month)}
				</span>
			</div>

			<button
				type="button"
				aria-label="Próximo mês"
				onClick={() => onChange(addMonths(month, 1))}
				className="grid size-9 place-items-center rounded-full border border-line bg-[var(--chip-bg)] text-ink-soft hover:text-ink hover:border-lagoon-deep/40"
			>
				<ChevronRight className="size-4" />
			</button>

			<button
				type="button"
				onClick={() => onChange(currentMonthKey())}
				disabled={isCurrent}
				className="ml-2 rounded-full border border-line bg-[var(--chip-bg)] px-3 py-1.5 text-xs font-semibold text-ink-soft hover:text-ink hover:border-lagoon-deep/40 disabled:opacity-0 disabled:pointer-events-none"
			>
				Hoje
			</button>
		</div>
	);
}
