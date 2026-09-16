import { LogOut, UserRound } from "lucide-react";
import type { AccountSummary } from "#/domain/auth/types";
import { signOut } from "#/store/auth-store";

export function AccountMenu({ user }: { user: AccountSummary }) {
	return (
		<div className="flex items-center gap-2 rounded-xl border border-line bg-[var(--chip-bg)] px-2.5 py-1.5">
			<span className="grid size-6 shrink-0 place-items-center rounded-full bg-palm-soft text-palm">
				<UserRound className="size-3.5" />
			</span>
			<span className="max-w-32 truncate text-sm font-semibold text-ink">
				{user.name}
			</span>
			<button
				type="button"
				onClick={signOut}
				title="Sair"
				className="inline-flex items-center gap-1 rounded-lg px-1.5 py-1 text-xs font-semibold text-ink-soft hover:text-ink"
			>
				<LogOut className="size-3.5" />
				Sair
			</button>
		</div>
	);
}
