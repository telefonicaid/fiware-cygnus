function fromSpaToEng(type, upperCase, singular) {
    if (type == 'Ratas') {
        if (upperCase) {
            if (singular)
                return 'Rat';
            else
                return 'Rats';
        } else {
            if (singular)
                return 'rat';
            else
                return 'rats';
        } // if else
    } else if (type == 'Ratones') {
        if (upperCase) {
            if (singular)
                return 'Mouse';
            else
                return 'Mice';
        } else {
            if (singular)
                return 'mouse';
            else
                return 'mice';
        } // if else
    } else if (type == 'Palomas') {
        if (upperCase) {
            if (singular)
                return 'Pigeon';
            else
                return 'Pigeons';
        } else {
            if (singular)
                return 'pigeon';
            else
                return 'pigeons';
        } // if else
    } else  if (type == 'Cucarachas') {
        if (upperCase) {
            if (singular)
                return 'Cockroach';
            else
                return 'Cockroaches';
        } else {
            if (singular)
                return 'cockroach';
            else
                return 'cockroaches';
        } // if else
    } else  if (type == 'Abejas') {
        if (upperCase) {
            if (singular)
                return 'Bee';
            else
                return 'Bees';
        } else {
            if (singular)
                return 'bee';
            else
                return 'bees';
        } // if else
    } else  if (type == 'Avispas') {
        if (upperCase) {
            if (singular)
                return 'Wasp';
            else
                return 'Wasps';
        } else {
            if (singular)
                return 'wasp';
            else
                return 'wasps';
        } // if else
    } else  if (type == 'Garrapatas') {
        if (upperCase) {
            if (singular)
                return 'Tick';
            else
                return 'Ticks';
        } else {
            if (singular)
                return 'tick';
            else
                return 'ticks';
        } // if else
    } else  if (type == 'Pulgas') {
        if (upperCase) {
            if (singular)
                return 'Flea';
            else
                return 'Fleas';
        } else {
            if (singular)
                return 'flea';
            else
                return 'fleas';
        } // if else                           
    } else
        return type;
} // fromSpaToEng