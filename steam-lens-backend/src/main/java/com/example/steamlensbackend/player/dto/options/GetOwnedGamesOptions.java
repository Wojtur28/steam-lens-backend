package com.example.steamlensbackend.player.dto.options;

import com.example.steamlensbackend.common.dto.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GetOwnedGamesOptions(
   Boolean includeAppInfo,
   Boolean includePlayedFreeGames,
   List<Long> appidsFilter,
   Boolean includeFreeSub,
   Boolean skipUnvettedApps,
   Language language,
   Boolean includeExtendedAppInfo
) {
    public static GetOwnedGamesOptions defaultOptions() {
        return  new GetOwnedGamesOptions(
                true,
                true,
                new ArrayList<>(),
                true,
                true, Language.POLISH,
                true
        );
    }

    public GetOwnedGamesOptions mergeWithDefaults(GetOwnedGamesOptions defaults) {
        return new GetOwnedGamesOptions(
                Objects.requireNonNullElse(this.includeAppInfo, defaults.includeAppInfo),
                Objects.requireNonNullElse(this.includePlayedFreeGames, defaults.includePlayedFreeGames),
                Objects.requireNonNullElse(this.appidsFilter, defaults.appidsFilter),
                Objects.requireNonNullElse(this.includeFreeSub, defaults.includeFreeSub),
                Objects.requireNonNullElse(this.skipUnvettedApps, defaults.skipUnvettedApps),
                Objects.requireNonNullElse(this.language, defaults.language),
                Objects.requireNonNullElse(this.includeExtendedAppInfo, defaults.includeExtendedAppInfo)
        );
    }
}
