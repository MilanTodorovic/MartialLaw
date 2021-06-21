# MartialLaw - a Starsector mini-mod
[![Github All Releases](https://img.shields.io/github/downloads/MilanTodorovic/MartialLaw/total.svg)]()

## “Sometimes you have to pick the gun up to put the gun down.”- Malcolm X

###TODO:
- Militarized light industry
  - reduces stability by **1**
  - produced goods can end up on the black market
  - increases likelihood of disruptions by pather cells
  - increases planets defence strength by **200**
- Light Armaments 
  - selling enough on a black market causes instability (based on the colonies stability and defenses) 
  - large quantities cause rebellions (disruption for **60** days)
- Armed Crew
  - double as crew and as marines
  - about **75%** strength in raids
  - **75%** payroll of marines
  - **cause mutiny (CR reduction)**
- Disarmed crew - ?
- Make modified "Recent unrest" and "organized_crime" in market_conditions.csv
- Add dialog option "Incentivize rebellion" to Colony interaction (higher chance to cause rebellions than with selling Light/Heavy Armaments to the black market)
  - with transponder reduces reputation by **10**
  - without transponder raises suspicion to Extreme
  - sell Light and Heavy Armament in stacks of **100/50**
  - sell Armed Crew in stacks of **50**
- Successful rebellions
  - cause stability penalty by **2**
  - reduces defenses by **100** for **60** days
  - interrupted mining/heavy industry/orbital works by **60** days
- Success of a rebellion depends on:
  - the stability:
    - 1-3 : **45%**
    - 4-6 : **20%**
    - 7-9 : **7%**
    - 10 : **1%**
  - military base/high command
  - proximity to other colonies (?)