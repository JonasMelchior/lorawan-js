#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    create role js_user with login password 'mighty_js_pwd';
    create database lorawan_js with owner js_user;
EOSQL