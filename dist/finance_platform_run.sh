#!/bin/bash

function usage()
{
    echo " Usage : "
    echo "   bash finance_platform_run.sh deploy"
    echo "   bash finance_platform_run.sh queryEntity account"
    echo "   bash finance_platform_run.sh queryTransaction id"
    echo "   bash finance_platform_run.sh register finance_platform finance_platform_credit "
    echo "   bash finance_platform_run.sh transfer from_finance_platform_account to_finance_platform_account amount "
    echo "   bash finance_platform_run.sh transactionUpload id acc1 acc2 money "
    echo "   bash finance_platform_run.sh transactionUpdate id money "
    echo "   bash finance_platform_run.sh splitFinanceTransaction  old_id new_id new_acc money "
    echo "   bash finance_platform_run.sh removeFinanceTransaction id"
    echo "   bash finance_platform_run.sh removeFinanceEntity acc"
    echo " "
    echo " "
    echo "examples : "
    echo "   bash finance_platform_run.sh deploy "
    echo "   bash finance_platform_run.sh register  Asset0  10000000 "
    echo "   bash finance_platform_run.sh register  Asset1  10000000 "
    echo "   bash finance_platform_run.sh transfer  Asset0  Asset1 11111 "
    echo "   bash finance_platform_run.sh queryEntity Asset0"
    echo "   bash finance_platform_run.sh queryEntity Asset1"
    exit 0
}

    case $1 in
    deploy)
            [ $# -lt 1 ] && { usage; }
            ;;
    register)
            [ $# -lt 3 ] && { usage; }
            ;;
    transfer)
            [ $# -lt 4 ] && { usage; }
            ;;
    queryEntity)
            [ $# -lt 2 ] && { usage; }
            ;;
    queryTransaction)
            [ $# -lt 2 ] && { usage; }
            ;;
    transactionUpload)
            [ $# -lt 5 ] && { usage; }
            ;;
    transactionUpdate)
            [ $# -lt 3 ] && { usage; }
            ;;
    splitFinanceTransaction)
            [ $# -lt 5 ] && { usage; }
            ;;
    removeFinanceEntity)
            [ $# -lt 2 ] && { usage; }
            ;;
    removeFinanceTransaction)
            [ $# -lt 2 ] && { usage; }
            ;;
    *)
        usage
            ;;
    esac

    java -Djdk.tls.namedGroups="secp256k1" -cp 'apps/*:conf/:lib/*' org.fisco.bcos.finance.client.finance_platform_client $@