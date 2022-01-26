# RW-HPS - Evolution

### Version specification

The version number of RW-HPS follows the [Semantic Version 2.0.0](https://semver.org/lang/zh-CN/#spec-item-9) specification.

In daily development, RW-HPS releases development preview versions with version suffixes such as `-dev1`, `-dev2`. These versions are only used for compatibility testing and other purposes and have no stability guarantee.

During major release development, RW-HPS releases milestone preview versions with the `-M1`, `-M2` suffixes. This represents the completion of a set of features that are not yet stable.  
The APIs added in these releases may still change in the next Milestone release, so please use them as needed.

RW-HPS releases the final preview version with the `-RC` version suffix before the major release.  
`-RC` means that the new version API has been finalized and is only a few internal optimizations or bug fixes away from the stable release.

### Version selection

**Stability**: stable (`x.y.z`) > release preview (`-RC`) > milestone preview (`-M`) > development (`-dev`).

| Purpose | Recommended to update to at least version |
|:------------------:|:--------------:|
| Production environment | `x.y.z` |
| Want to experience stable new features as soon as possible | `-RC` |
| I want to experience the new features anyway | `-M` |
| Submit PR | `-dev` |

## Update Compatibility

For `x.y.z` version numbers:
- When `z` is added, there will only be bug fixes, and new functions added as necessary (to fix an issue), no breaking changes.
- When `y` is added, there may be new APIs introduced, and old APIs deprecated. But these deprecations are removed (hidden) only after a deprecation cycle. Downward compatibility is guaranteed.
- When `x` is added, any API may change. No compatibility guarantees.